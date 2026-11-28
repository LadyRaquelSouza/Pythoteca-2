// Independent adapter for synthetic tests; upstream GPL source stays separate.
import fs from 'node:fs';
import path from 'node:path';
import { pathToFileURL } from 'node:url';
const root = process.argv[3] || '/upstream';
const { default: PortugolRuntime } = await import(pathToFileURL(path.join(root, 'src/compiler/vm/portugolrun.js')));
const corpus = JSON.parse(fs.readFileSync(new URL('./corpus.json', import.meta.url), 'utf8'));
const fixture = corpus.cases.find(c => c.id === process.argv[2]);
if (!fixture) throw new Error('Unknown fixture');
const started = performance.now();
const diagnostics = [];
const input = fixture.input.split('\n');
if (input.at(-1) === '') input.pop();
let value = '';
const consoleAdapter = {
    get value() { return value; },
    set value(v) {
        if (Buffer.byteLength(String(v)) > 65536) throw new Error('output budget exceeded');
        value = String(v);
    },
    leia: async () => {
        if (!input.length) throw new Error('fixture input exhausted');
        return input.shift() + '\n';
    }
};
const runtime = new PortugolRuntime(consoleAdapter);
runtime.escrever_tempo = false;
runtime.escrever_erros = false;
runtime.escrever_debug = false;
// No optional libraries, graphics, network, Android bridge or turbo mode.
const report = e => diagnostics.push({type: String(e.myErrorType || ''), text: String(e.text || '').slice(0, 1000)});
const finish = status => process.stdout.write(JSON.stringify({
    id: fixture.id, status, stdout: value, errors: diagnostics,
    elapsedMs: Math.round(performance.now() - started),
    outputIncludesInputEcho: true
}) + '\n');
try {
    const compiled = runtime.compilar(fixture.source, report, false);
    if (!compiled.success) finish('syntax_or_compile_error');
    else {
        await runtime.executar(fixture.source, compiled, report, false);
        finish(diagnostics.length ? 'runtime_error' : 'completed');
    }
} catch (e) {
    diagnostics.push({type: 'adapter_or_runtime_exception', text: String(e).slice(0, 1000)});
    finish('process_error');
}

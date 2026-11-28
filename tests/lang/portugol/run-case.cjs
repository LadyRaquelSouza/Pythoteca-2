// Only synthetic fixtures, in a disposable offline process.
const fs = require('node:fs');
const path = require('node:path');
const { LexadorPortugolStudio, AvaliadorSintaticoPortugolStudio,
    InterpretadorPortugolStudio } = require('@designliquido/portugol-studio');
const corpus = JSON.parse(fs.readFileSync(path.join(__dirname, 'corpus.json'), 'utf8'));
const fixture = corpus.cases.find(c => c.id === process.argv[2]);
if (!fixture) throw new Error('Unknown fixture');
async function main() {
    const started = performance.now();
    const lex = new LexadorPortugolStudio().mapear(fixture.source.split('\n'), -1);
    const finish = (status, stdout, errors) => {
        process.stdout.write(JSON.stringify({id: fixture.id, status, stdout, errors,
            elapsedMs: Math.round(performance.now() - started)}) + '\n');
    };
    const diagnostics = errors => (errors || []).map(e => String(e.erroInterno?.message || e.message || e.mensagem || e).slice(0, 1000));
    if (lex.erros?.length) return finish('syntax_error', '', diagnostics(lex.erros));
    const parsed = await new AvaliadorSintaticoPortugolStudio().analisar(lex, -1);
    if (parsed.erros?.length) return finish('syntax_error', '', diagnostics(parsed.erros));
    let output = '';
    const emit = value => {
        const chunk = String(value);
        if (Buffer.byteLength(output) + Buffer.byteLength(chunk) > 65536) throw new Error('output budget exceeded');
        output += chunk;
    };
    const engine = new InterpretadorPortugolStudio(process.cwd(), false, emit, () => {});
    engine.funcaoDeRetorno = emit;
    engine.funcaoDeRetornoMesmaLinha = emit;
    const input = fixture.input.split('\n');
    if (input.at(-1) === '') input.pop();
    engine.interfaceEntradaSaida = {question: (_, callback) => {
        if (!input.length) throw new Error('fixture input exhausted');
        callback(input.shift());
    }};
    const result = await engine.interpretar(parsed.declaracoes);
    finish(result.erros?.length ? 'runtime_error' : 'completed', output, diagnostics(result.erros));
}
main().catch(e => { process.stderr.write(String(e).slice(0, 1000)); process.exitCode = 1; });

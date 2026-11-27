def answer():
    return 6 * 7


def fail():
    raise ValueError("intentional probe failure")


def network_denied():
    import socket
    import errno
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.settimeout(2)
            sock.connect(("192.0.2.1", 443))
    except OSError as exc:
        if exc.errno in (errno.EACCES, errno.EPERM):
            return 1
        raise
    return 0


def endless():
    while True:
        pass

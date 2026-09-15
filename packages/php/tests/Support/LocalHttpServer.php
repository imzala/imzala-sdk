<?php

declare(strict_types=1);

namespace Imzala\Tests\Support;

/**
 * A real HTTP server (PHP's built-in web server in a child process) that
 * records every request and answers from a scripted list of replies. Lets
 * a test run the vendored generated client and Guzzle end to end, so what
 * is asserted is what actually went over the wire.
 *
 * @phpstan-type Reply array{status:int, body:mixed, headers?:array<string,string>}
 */
final class LocalHttpServer
{
    /** @var resource */
    private $process;
    private string $dir;
    public readonly string $baseUrl;

    /**
     * @param list<Reply> $replies the n-th request receives the n-th reply; the last one repeats
     */
    public function __construct(array $replies)
    {
        $this->dir = sys_get_temp_dir() . '/imzala-local-server-' . bin2hex(random_bytes(6));
        mkdir($this->dir, 0700, true);
        file_put_contents($this->dir . '/replies.json', json_encode($replies, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR));
        file_put_contents($this->dir . '/requests.jsonl', '');

        $port = self::freePort();
        $this->baseUrl = 'http://127.0.0.1:' . $port;

        $command = [
            PHP_BINARY,
            '-d', 'variables_order=EGPCS',
            '-S', '127.0.0.1:' . $port,
            __DIR__ . '/local-server-router.php',
        ];
        $process = proc_open(
            $command,
            [1 => ['file', $this->dir . '/server.log', 'a'], 2 => ['file', $this->dir . '/server.log', 'a']],
            $pipes,
            null,
            ['IMZALA_LOCAL_SERVER_DIR' => $this->dir, 'PATH' => (string) getenv('PATH')],
        );
        if ($process === false) {
            throw new \RuntimeException('could not start the local HTTP server');
        }
        $this->process = $process;
        $this->waitUntilListening($port);
    }

    /**
     * Requests seen so far, oldest first.
     *
     * @return list<array{method:string, uri:string, headers:array<string,string>, body:string, form:array<string,string>, files:array<string,array{name:string,type:string,content:string}>}>
     */
    public function requests(): array
    {
        $lines = file($this->dir . '/requests.jsonl', FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES) ?: [];
        return array_values(array_map(static fn (string $line) => json_decode($line, true, 512, JSON_THROW_ON_ERROR), $lines));
    }

    public function stop(): void
    {
        if (is_resource($this->process)) {
            proc_terminate($this->process, 9);
            proc_close($this->process);
        }
        foreach (glob($this->dir . '/*') ?: [] as $file) {
            @unlink($file);
        }
        @rmdir($this->dir);
    }

    private static function freePort(): int
    {
        $socket = stream_socket_server('tcp://127.0.0.1:0', $errno, $errstr);
        if ($socket === false) {
            throw new \RuntimeException('no free port: ' . $errstr);
        }
        $name = stream_socket_get_name($socket, false);
        fclose($socket);
        return (int) substr((string) $name, (int) strrpos((string) $name, ':') + 1);
    }

    private function waitUntilListening(int $port): void
    {
        $deadline = microtime(true) + 10;
        while (microtime(true) < $deadline) {
            $probe = @fsockopen('127.0.0.1', $port, $errno, $errstr, 0.2);
            if ($probe !== false) {
                fclose($probe);
                return;
            }
            usleep(20_000);
        }
        throw new \RuntimeException('local HTTP server did not start: ' . (string) file_get_contents($this->dir . '/server.log'));
    }
}

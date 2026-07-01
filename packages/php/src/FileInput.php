<?php

declare(strict_types=1);

namespace Imzala;

use RuntimeException;
use SplFileObject;

/**
 * PHP-native file input for multipart endpoints ({@see
 * DemandsResource::uploadDocument}, {@see TimestampsResource::create}).
 * PHP servers hold uploaded file bytes as a string (from
 * {@code $_FILES[...]['tmp_name']} read via {@code file_get_contents},
 * an upstream download, ...) — the facade accepts bytes + filename and
 * materializes the vendored generated client's required input itself.
 *
 * <p><b>Why a temp file (the one filesystem-dependent step in this
 * vertical — same root cause Java hit):</b> the vendored generated
 * client's multipart layer ({@code FormDataProcessor::processFiles()})
 * only accepts a {@see \Psr\Http\Message\StreamInterface}, a raw PHP
 * resource, or a {@see SplFileObject} — and for the {@see SplFileObject}
 * case (what this class produces) it calls {@code
 * Utils::tryFopen($file->getRealPath(), 'rb')}, i.e. it re-opens the file
 * from its path on disk and derives the multipart part's filename from
 * that path's basename. A bare in-memory resource (e.g. {@code
 * fopen('php://temp', 'r+')}) *is* accepted by the same code path, but
 * Guzzle's own {@code MultipartStream} then can't derive a filename from
 * a non-local stream URI, and the imzala.org server infers processing
 * (PDF vs. image vs. office doc) from the filename's extension — so a
 * nameless part would silently misprocess. {@see self::toSplFileObject()}
 * therefore writes {@see self::$content} to a throwaway temp file named
 * after {@see self::$fileName} (fresh temp dir per file, to avoid
 * filename collisions between sibling files in the same multi-file
 * upload). Callers of the facade never touch a temp file directly —
 * {@see DemandsResource} and {@see TimestampsResource} create + delete it
 * (in a {@code finally} block) for the duration of a single call.
 *
 * <p>{@see self::$contentType} is best-effort only and NOT sent as the
 * multipart part's actual Content-Type: the generated client's multipart
 * builder never threads a per-file Content-Type through either — the
 * server infers processing from {@see self::$fileName}'s extension
 * either way, same as every other imzala SDK (this field exists for API
 * symmetry with the other language SDKs and for forward compatibility,
 * not because it's currently sent over the wire).
 */
final class FileInput
{
    private string $content;
    private string $fileName;
    private ?string $contentType;

    public function __construct(string $content, string $fileName, ?string $contentType = null)
    {
        if ($fileName === '') {
            throw new \InvalidArgumentException('FileInput: fileName is required.');
        }
        $this->content = $content;
        $this->fileName = $fileName;
        $this->contentType = $contentType;
    }

    public function getContent(): string
    {
        return $this->content;
    }

    public function getFileName(): string
    {
        return $this->fileName;
    }

    public function getContentType(): ?string
    {
        return $this->contentType;
    }

    /**
     * Writes {@see self::$content} to a fresh temp directory (one per
     * call, to avoid filename collisions between sibling files in the
     * same upload) under a file literally named {@see self::$fileName}.
     * Caller owns cleanup of both the file and its parent temp directory
     * — see {@see self::cleanupTempPath()}.
     */
    public function toSplFileObject(): SplFileObject
    {
        $baseName = basename($this->fileName);
        $dir = sys_get_temp_dir() . DIRECTORY_SEPARATOR . 'imzala-upload-' . bin2hex(random_bytes(8));

        if (!mkdir($dir, 0700, true) && !is_dir($dir)) {
            throw new RuntimeException("FileInput: failed to create temp directory '{$dir}'.");
        }

        $path = $dir . DIRECTORY_SEPARATOR . $baseName;
        if (file_put_contents($path, $this->content) === false) {
            throw new RuntimeException("FileInput: failed to write temp file '{$path}'.");
        }

        return new SplFileObject($path, 'rb');
    }

    /** Best-effort cleanup of a path produced by {@see self::toSplFileObject()} — deletes the file and its parent temp dir. */
    public static function cleanupTempPath(string $path): void
    {
        $dir = dirname($path);
        if (is_file($path)) {
            @unlink($path);
        }
        if (is_dir($dir)) {
            @rmdir($dir);
        }
    }
}

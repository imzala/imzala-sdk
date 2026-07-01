<?php

declare(strict_types=1);

namespace Imzala;

use InvalidArgumentException;

/** Parameters for {@see TimestampsResource::create}. */
final class CreateTimestampParams
{
    private string $content;
    private string $fileName;
    private ?string $contentType = null;
    private ?string $idempotencyKey = null;
    private ?string $description = null;
    private ?string $ownerFirstName = null;
    private ?string $ownerLastName = null;

    public function __construct(string $content, string $fileName)
    {
        if ($fileName === '') {
            throw new InvalidArgumentException('CreateTimestampParams: fileName is required.');
        }
        $this->content = $content;
        $this->fileName = $fileName;
    }

    public function withContentType(?string $contentType): self
    {
        $this->contentType = $contentType;
        return $this;
    }

    /** Client-generated idempotency key (UUID recommended) — replays within 5 minutes return the original result without spending a credit. */
    public function withIdempotencyKey(?string $idempotencyKey): self
    {
        $this->idempotencyKey = $idempotencyKey;
        return $this;
    }

    public function withDescription(?string $description): self
    {
        $this->description = $description;
        return $this;
    }

    public function withOwnerFirstName(?string $ownerFirstName): self
    {
        $this->ownerFirstName = $ownerFirstName;
        return $this;
    }

    public function withOwnerLastName(?string $ownerLastName): self
    {
        $this->ownerLastName = $ownerLastName;
        return $this;
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

    public function getIdempotencyKey(): ?string
    {
        return $this->idempotencyKey;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function getOwnerFirstName(): ?string
    {
        return $this->ownerFirstName;
    }

    public function getOwnerLastName(): ?string
    {
        return $this->ownerLastName;
    }
}

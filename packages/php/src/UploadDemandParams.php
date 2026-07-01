<?php

declare(strict_types=1);

namespace Imzala;

use InvalidArgumentException;

/** Parameters for {@see DemandsResource::uploadDocument} — creates a demand directly from an uploaded document (no template). */
final class UploadDemandParams
{
    /** @var FileInput[] */
    private array $files;
    /** @var UploadPartyInput[] */
    private array $parties;
    /** @var int[]|null */
    private ?array $order = null;
    private ?string $title = null;
    private ?string $description = null;

    /**
     * @param FileInput[] $files one document OR 1-20 images — merged server-side into a single PDF
     * @param UploadPartyInput[] $parties signing parties
     */
    public function __construct(array $files, array $parties)
    {
        if ($files === []) {
            throw new InvalidArgumentException('UploadDemandParams: files is required and must be non-empty.');
        }
        if ($parties === []) {
            throw new InvalidArgumentException('UploadDemandParams: parties is required and must be non-empty.');
        }
        $this->files = $files;
        $this->parties = $parties;
    }

    /** Reorders a multi-image upload — indices into {@see self::getFiles()}. */
    public function withOrder(array $order): self
    {
        $this->order = $order;
        return $this;
    }

    public function withTitle(?string $title): self
    {
        $this->title = $title;
        return $this;
    }

    public function withDescription(?string $description): self
    {
        $this->description = $description;
        return $this;
    }

    /** @return FileInput[] */
    public function getFiles(): array
    {
        return $this->files;
    }

    /** @return UploadPartyInput[] */
    public function getParties(): array
    {
        return $this->parties;
    }

    /** @return int[]|null */
    public function getOrder(): ?array
    {
        return $this->order;
    }

    public function getTitle(): ?string
    {
        return $this->title;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }
}

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
    private ?string $idempotencyKey = null;
    private ?string $fieldTemplateId = null;
    private ?string $onAnchorMiss = null;
    private ?string $sendInvitations = null;
    private bool $force = false;

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

    /**
     * Makes the upload safe to retry: a second request with the same key does
     * not create a second demand. With a key, one retry is made after a 429.
     */
    public function withIdempotencyKey(?string $idempotencyKey): self
    {
        $this->idempotencyKey = $idempotencyKey;
        return $this;
    }

    /**
     * Field template ({@code kind: FIELD_LAYOUT}) whose layout is applied to
     * the upload. The upload must then be a single PDF, and every party needs
     * {@see UploadPartyInput::$templatePartyId}. The layout is resolved before
     * the demand is created or credit is spent; a 422 creates nothing.
     * Dry-run first with {@see FieldTemplatesResource::previewLayout()}.
     */
    public function withFieldTemplateId(?string $fieldTemplateId): self
    {
        $this->fieldTemplateId = $fieldTemplateId;
        return $this;
    }

    /**
     * {@code 'block'} or {@code 'drop'}; only with a field template. Can
     * tighten the template, never relax it: omitted means {@code block};
     * {@code drop} applies only if every affected field is already set to
     * drop in the template (otherwise {@code block} is used and the response
     * carries an {@code ON_ANCHOR_MISS_NOT_RELAXED} warning). Signature fields
     * are never dropped.
     */
    public function withOnAnchorMiss(?string $onAnchorMiss): self
    {
        $this->onAnchorMiss = $onAnchorMiss;
        return $this;
    }

    /**
     * Sends signing invitations in the same request. <b>Off by default on
     * this endpoint.</b> {@code 'true'} or {@code 'all'} uses every channel,
     * {@code 'email'} limits it to e-mail, {@code 'sms'} to phone channels
     * (SMS and WhatsApp), {@code 'false'} sends nothing. It can only narrow:
     * a channel switched off in the demand's or party's notification
     * settings is not turned back on.
     */
    public function withSendInvitations(?string $sendInvitations): self
    {
        $this->sendInvitations = $sendInvitations;
        return $this;
    }

    /**
     * Deliberately bypass the duplicate check ({@code DUPLICATE_SUSPECTED}).
     * Only meaningful for calls without an idempotency key.
     */
    public function withForce(bool $force): self
    {
        $this->force = $force;
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

    public function getIdempotencyKey(): ?string
    {
        return $this->idempotencyKey;
    }

    public function getFieldTemplateId(): ?string
    {
        return $this->fieldTemplateId;
    }

    public function getOnAnchorMiss(): ?string
    {
        return $this->onAnchorMiss;
    }

    public function getSendInvitations(): ?string
    {
        return $this->sendInvitations;
    }

    public function getForce(): bool
    {
        return $this->force;
    }
}

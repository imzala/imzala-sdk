<?php

declare(strict_types=1);

namespace Imzala;

/** One signing party for {@see DemandsResource::uploadDocument}. Email or phone (or both) required per party. */
final class UploadPartyInput
{
    public function __construct(
        public readonly ?string $firstName = null,
        public readonly ?string $lastName = null,
        public readonly ?string $email = null,
        /** E.164 format (e.g. {@code "+905551112233"}). */
        public readonly ?string $phone = null,
        /**
         * Field template role for this party. Required on every party when
         * {@see UploadDemandParams::withFieldTemplateId()} is set; each role
         * of the template must be mapped exactly once.
         */
        public readonly ?string $templatePartyId = null,
    ) {
    }

    /** @return array<string, string> */
    public function toArray(): array
    {
        $result = [];
        if ($this->firstName !== null) {
            $result['first_name'] = $this->firstName;
        }
        if ($this->lastName !== null) {
            $result['last_name'] = $this->lastName;
        }
        if ($this->email !== null) {
            $result['email'] = $this->email;
        }
        if ($this->phone !== null) {
            $result['phone'] = $this->phone;
        }
        if ($this->templatePartyId !== null) {
            $result['template_party_id'] = $this->templatePartyId;
        }
        return $result;
    }
}

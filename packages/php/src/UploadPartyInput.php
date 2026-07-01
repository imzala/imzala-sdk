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
        /** E.164 format (e.g. {@code "+905551234567"}). */
        public readonly ?string $phone = null,
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
        return $result;
    }
}

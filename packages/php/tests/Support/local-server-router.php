<?php

/**
 * Router for the PHP built-in web server used by the envelope tests.
 *
 * Every request is appended to `requests.jsonl` in the directory named by
 * the IMZALA_LOCAL_SERVER_DIR environment variable; the reply comes from
 * `replies.json` in the same directory (the n-th request gets the n-th
 * reply, the last reply repeats).
 */

declare(strict_types=1);

$dir = getenv('IMZALA_LOCAL_SERVER_DIR');
if ($dir === false || $dir === '') {
    http_response_code(500);
    echo 'IMZALA_LOCAL_SERVER_DIR is not set';
    return true;
}

$headers = [];
foreach ($_SERVER as $name => $value) {
    if (str_starts_with($name, 'HTTP_')) {
        $headers[strtolower(str_replace('_', '-', substr($name, 5)))] = $value;
    }
}
if (isset($_SERVER['CONTENT_TYPE'])) {
    $headers['content-type'] = $_SERVER['CONTENT_TYPE'];
}

$files = [];
foreach ($_FILES as $field => $file) {
    $files[$field] = [
        'name' => $file['name'],
        'type' => $file['type'],
        'content' => is_string($file['tmp_name']) && $file['tmp_name'] !== '' ? (string) file_get_contents($file['tmp_name']) : '',
    ];
}

$record = [
    'method' => $_SERVER['REQUEST_METHOD'],
    'uri' => $_SERVER['REQUEST_URI'],
    'headers' => $headers,
    'body' => (string) file_get_contents('php://input'),
    'form' => $_POST,
    'files' => $files,
];

$requestsFile = $dir . '/requests.jsonl';
$handle = fopen($requestsFile, 'a+');
flock($handle, LOCK_EX);
fwrite($handle, json_encode($record, JSON_UNESCAPED_UNICODE) . "\n");
fflush($handle);
rewind($handle);
$index = 0;
while (($line = fgets($handle)) !== false) {
    if (trim($line) !== '') {
        $index++;
    }
}
flock($handle, LOCK_UN);
fclose($handle);

$replies = json_decode((string) file_get_contents($dir . '/replies.json'), true);
$reply = $replies[min($index - 1, count($replies) - 1)];

http_response_code((int) $reply['status']);
header('Content-Type: application/json');
foreach ($reply['headers'] ?? [] as $name => $value) {
    header($name . ': ' . $value);
}
echo json_encode($reply['body'], JSON_UNESCAPED_UNICODE);
return true;

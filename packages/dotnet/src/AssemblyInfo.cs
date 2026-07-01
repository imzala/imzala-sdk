using System.Runtime.CompilerServices;

// Lets the test project construct resource classes directly (internal
// constructors) and unit-test ErrorMapper.Map(...) without going through a
// live HTTP call — mirrors B1/B2's approach of mocking/patching the generated
// client at the method level rather than spinning up a mock HTTP server.
[assembly: InternalsVisibleTo("Imzala.Tests")]

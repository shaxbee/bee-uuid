bee-uuid
========

Java UUID RFC4122 v3/v4/v5 implementation.

Interface is inspired by Python 'uuid' module.
Code requires Java 7 and does not carry external dependencies.

Name-based v3 (MD5) and v5 (SHA-1) are supported, java.security.MessageDigest is providing hashning algorithms.

Random-based v4 is backed by java.security.SecureRandom with SHA1PRNG algorithm.



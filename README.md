# PGP Benchmark

An Android application for comparing BouncyCastle's pure Java OpenPGP
implementation, and the GnuPG native implementation.

Exciting!

## Setup

The benchmark requires

1) recipient public key (to encrypt to)
2) sender private key (to sign)
3) data to sign/encrypt

The keys are located in `keys/` inside this repo, but the data file needs to be
generated.


1. Plug in a device in debug mode
2. Push the generated files to your device/emulator, generating them if needed

        make deploy

3. Run the test from the CLI

        make test

    You can run it multiple times to get more data points.

4. To fetch the test results:

        make fetch-results

It assumes adb is in your `PATH` and that there's only one device connected. If
you've a more advanced adb setup, then `cat Makefile` and figure it out.


all: keys/test_100M.dat pgp-keys


keys/test_100M.dat:
	@echo "generating 100 megabyte data file" && dd if=/dev/urandom of=keys/test_100M.dat bs=1024K count=100

deploy:
	adb push keys/pgpbenchmark-sender.sec /sdcard/pgpbenchmark-sender.asc
	adb push keys/pgpbenchmark-recipient.pub /sdcard/pgpbenchmark-recipient.pub.asc

pgp-keys:
	cd keys/ && test -s pgpbenchmark-recipient.sec || GNUPGHOME=. gpg2 --batch --gen-key receiver.params
	cd keys/ && test -s pgpbenchmark-sender.sec || GNUPGHOME=. gpg2 --batch --gen-key sender.params


install:
	ant debug install

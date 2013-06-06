all: keys/test_100M.dat build


keys/test_100M.dat:
	@echo "generating 100 megabyte data file" && dd if=/dev/urandom of=keys/test_100M.dat bs=1024K count=100

deploy: keys/test_100M.dat
	adb push keys/pgpbenchmark-sender.sec /sdcard/pgpbenchmark-sender.asc
	adb push keys/pgpbenchmark-recipient.pub /sdcard/pgpbenchmark-recipient.pub.asc
	adb shell ls /sdcard/test_100M.dat || adb push keys/test_100M.dat /sdcard/

build:
	ant debug

install:
	ant debug install

test: deploy
	ant instrument install test
	echo
	echo
	adb shell cat /sdcard/pgpbenchmark-report.txt

clean:
	ant clean
	adb shell rm /sdcard/pgpbenchmark-report.txt

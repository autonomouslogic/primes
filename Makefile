.PHONY: prime-search test format clean

test:
	./gradlew test --stacktrace

format:
	./gradlew spotlessApply --stacktrace

clean:
	./gradlew clean --stacktrace

prime-search:
	./gradlew primeSearch --stacktrace

{
  lib,
  fetchFromGitHub,
  jre,
  makeWrapper,
  maven,
}: let
  litterboxMlVersion = "1.0-SNAPSHOT";
in
  maven.buildMavenPackage rec {
    pname = "embedded-kittens";
    version = litterboxMlVersion;

    src = ./.;

    mvnHash = "sha256-RPn+xpZdNpEeU0MCZI+4Syuq2ufdkFQFaJ1MuubBleI=";

    nativeBuildInputs = [makeWrapper];

    installPhase = ''
      mkdir -p $out/share/java
      install -Dm644 target/embedded-kittens-${version}.full.jar $out/share/java/embedded-kittens-${version}.jar

      makeWrapper ${jre}/bin/java $out/bin/embedded-kittens \
        --add-flags "-jar $out/share/java/embedded-kittens-${version}.jar"
    '';

    meta = with lib; {
      licence = [licenses.gpl3Plus];
    };
  }

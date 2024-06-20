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
    pname = "litterbox-ml";
    version = litterboxMlVersion;

    src = ./.;

    mvnHash = "sha256-gyE1RbmDzHIf+/2iVvFpoaDgGnY2WSKCbYw6zyubYDs=";

    nativeBuildInputs = [makeWrapper];

    installPhase = ''
      mkdir -p $out/share/java
      install -Dm644 target/litterbox-ml-${version}.full.jar $out/share/java/litterbox-ml-${version}.jar

      makeWrapper ${jre}/bin/java $out/bin/litterbox-ml \
        --add-flags "-jar $out/share/java/litterbox-ml-${version}.jar"
    '';

    meta = with lib; {
      licence = [licenses.gpl3Plus];
    };
  }

{
  description = "Nim Game API (Kotlin + Spring Boot) - Isolated Dev Environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            jdk25
            just
            ktlint
          ];

          shellHook = ''
            export GRADLE_USER_HOME="$PWD/.gradle-home"
            echo "-> Isolated JVM/Kotlin environment ready."
          '';
        };
      }
    );
}

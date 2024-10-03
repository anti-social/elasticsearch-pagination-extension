{ pkgs ? import <nixpkgs> {} }:

with pkgs;

(buildFHSUserEnv {
  name = "elasticsearch-grouping-mixup-rescorer";
  targetPkgs = pkgs: [
    (bashInteractive.override { forFHSEnv = true; })
  ];
  runScript = pkgs.writeScript "init.sh" ''
    export JAVA_HOME="${pkgs.jdk11}/lib/openjdk"
    exec bash
  '';
}).env

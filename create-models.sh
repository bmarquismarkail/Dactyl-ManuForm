#!/usr/bin/env bash

set -euo pipefail

OPENSCAD="${OPENSCAD:-openscad}"

render_stl() {
    "$OPENSCAD" --backend Manifold --export-format asciistl -o "$1" "$2"
    python3 scripts/check-stl.py "$1"
}

render_dxf() {
    "$OPENSCAD" --backend Manifold -o "$1" "$2"
}

lein run src/dactyl_keyboard/dactyl.clj
cp things/right.scad things/right-4x5.scad
cp things/left.scad things/left-4x5.scad
cp things/right-plate.scad things/right-4x5-plate.scad
render_dxf things/right-4x5-plate.dxf things/right-4x5-plate.scad
render_stl things/right-4x5.stl things/right-4x5.scad
render_stl things/left-4x5.stl things/left-4x5.scad

patch -p1 < 4x6.patch 
lein run src/dactyl_keyboard/dactyl.clj
cp things/right.scad things/right-4x6.scad
cp things/left.scad things/left-4x6.scad
cp things/right-plate.scad things/right-4x6-plate.scad
render_dxf things/right-4x6-plate.dxf things/right-4x6-plate.scad
render_stl things/right-4x6.stl things/right-4x6.scad
render_stl things/left-4x6.stl things/left-4x6.scad
git checkout src/dactyl_keyboard/dactyl.clj

patch -p1 < 5x6.patch 
lein run src/dactyl_keyboard/dactyl.clj
cp things/right.scad things/right-5x6.scad
cp things/left.scad things/left-5x6.scad
cp things/right-plate.scad things/right-5x6-plate.scad
render_dxf things/right-5x6-plate.dxf things/right-5x6-plate.scad
render_stl things/right-5x6.stl things/right-5x6.scad
render_stl things/left-5x6.stl things/left-5x6.scad
git checkout src/dactyl_keyboard/dactyl.clj

patch -p1 < 6x6.patch 
lein run src/dactyl_keyboard/dactyl.clj
cp things/right.scad things/right-6x6.scad
cp things/left.scad things/left-6x6.scad
cp things/right-plate.scad things/right-6x6-plate.scad
render_dxf things/right-6x6-plate.dxf things/right-6x6-plate.scad
render_stl things/right-6x6.stl things/right-6x6.scad
render_stl things/left-6x6.stl things/left-6x6.scad
git checkout src/dactyl_keyboard/dactyl.clj


# git add things/*-4x5.stl
# git add things/right-4x5-plate.dxf
# git commit -m "Add CAD files"

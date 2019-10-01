#!/bin/bash

build() {
    local mdPath cssRelat
    #
    mdPath=$(realpath --relative-to=$(pwd)/$1 ../$1)
    cssRelat=$(realpath --relative-to=$(pwd)/$1 ${cssfile})
    #
    [ ! -d "$1" ] && mkdir "$1"
    cd $1
    #
    for f in $(ls -1 ${mdPath} | grep "\.md$" | sed -e "s/\.md$//"); do
	pandoc -s -t html5 -c ${cssRelat} ${mdPath}/${f}.md -o ${f}.html
    done
    cd ${rootdir}
}

rootdir=$(cd $(dirname $0)/doc && pwd)
cssfile=${rootdir}/github.css
cd ${rootdir}

# index
build .

# sect 0 
build sect0

# sect 1
build sect1

# sect 2
build sect2

# sect 3
build sect3

# sect 4
build sect4

# sect 5
build sect5

# sect 6
build sect6

#!/bin/bash

# Prerequisites
#  1. Pandoc
#     https://pandoc.org/installing.html
#  2. GNU core utilities
#     https://www.gnu.org/software/coreutils

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
	sed -e "s/index\.md/index.html/" ${mdPath}/${f}.md  | \
	    pandoc -s -t html5 -c ${cssRelat} -o ${f}.html \
		   --metadata pagetitle="Beam Programming Guide"
	sed -i "" \
	    -e "s/pre class=\"sourceCode java\"/pre class=\"line-numbers\"/g" \
	    -e "s/code class=\"sourceCode java\"/code class=\"language-java\"/g" \
	    -e "s/pre class=\"sourceCode bash\"/pre class=\"line-numbers\"/g" \
	    -e "s/code class=\"sourceCode bash\"/code class=\"language-bash\"/g" \
	    -e "s/pre class=\"sourceCode xml\"/pre class=\"line-numbers\"/g" \
	    -e "s/code class=\"sourceCode xml\"/code class=\"language-xml\"/g" \
	    -e "s/<\/body>/<script src=\"..\/code.js\"><\/script><\/body>/" \
	    ${f}.html
    done
    #
    # images
    if [ -d  "${mdPath}/figs" ]; then
	[ ! -d figs ] && mkdir figs
	cp -r ${mdPath}/figs/* ./figs
    fi

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

# # sect 4
# build sect4

# # sect 5
# build sect5

# # sect 6
# build sect6

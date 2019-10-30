#!/bin/bash

# Prerequisites
#  1. Pandoc
#     https://pandoc.org/installing.html
#  2. GNU core utilities
#     https://www.gnu.org/software/coreutils

#--------------------------------------------------
outputDir=doc

build() {
    local opt mdPath cssRelat ifile ofile dname depth pathes
    #
    mdPath=$(realpath --relative-to=$(pwd)/$1 $(git rev-parse --show-toplevel)/md/$1)
    [ $# -eq 2 ] && opt="-maxdepth $2"
    #
    if [ ! -z "$1" ]; then
	[ ! -d "$1" ] && mkdir -p "$1"
	cd $1
    fi

    pathes=()
    for ifile in $(find ${mdPath} -type f -name "*.md" ${opt}); do
	ofile=$(realpath --relative-to=${mdPath} ${ifile} | sed "s/\.md/.html/")
	dname=$(dirname $ofile)
	cssRelat=$(realpath --relative-to=${dname} ${cssfile})

	[ "${dname}" != "." ] && [ ! -d "${dname}" ] && mkdir -p ${dname}

	echo "<script src=\"$(realpath --relative-to=${dname} ${jsfile})\"></script>" > tmp
	sed -e "s/\.md/.html/" ${ifile}  | \
	    pandoc -s -t html5 -c ${cssRelat} -o ${ofile} -H tmp \
		   --metadata pagetitle="Ja: Beam Programming Guide"
	rm tmp
	sed -i "" \
	    -e "s/pre class=\"sourceCode java\"/pre class=\"line-numbers\"/g" \
	    -e "s/code class=\"sourceCode java\"/code class=\"language-java\"/g" \
	    -e "s/pre class=\"sourceCode bash\"/pre class=\"line-numbers\"/g" \
	    -e "s/code class=\"sourceCode bash\"/code class=\"language-bash\"/g" \
	    -e "s/pre class=\"sourceCode xml\"/pre class=\"line-numbers\"/g" \
	    -e "s/code class=\"sourceCode xml\"/code class=\"language-xml\"/g" \
	    -e "s/pre class=\"sourceCode json\"/pre class=\"line-numbers\"/g" \
	    -e "s/code class=\"sourceCode json\"/code class=\"language-json\"/g" \
	    ${ofile}
	#
	pathes+=("${dname}")
    done

    # images
    local from to
    for dname in ${pathes[@]}; do
	from=$(cd ${mdPath} && cd ${dname} && pwd)/figs
	if [ -d  "${from}" ]; then
	    [ ! -d ${dname}/figs ] && mkdir -p ${dname}/figs
	    cp ${from}/* ${dname}/figs/
	fi
    done

    cd ${docdir}
}

#--------------------------------------------------
rootdir=$(cd $(dirname $0) && pwd)
docdir="${rootdir}/${outputDir}"
[ ! -d "${docdir}" ] && mkdir -p ${docdir}
cd ${docdir}
#
cp ../.tool/github.css .
cp ../.tool/prism.js .

cssfile=${docdir}/github.css
jsfile=${docdir}/prism.js

# index
build "" 1

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

# sect 7
build sect7

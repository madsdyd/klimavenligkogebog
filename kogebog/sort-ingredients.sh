#!/bin/bash

function die() {
    echo "Error" "$@"
    exit 1    
}



jq 'sort_by(.id)' ingredients.json | tr "\n" "#" | sed 's/},#/¤¤¤/g' | sed 's/#  //g' | sed 's/¤¤¤/},\n/g' | sed 's/#/\n/g' | sed 's/\[{/\[\n  {/g' > ged.json || die "Unable to format ingredients.json"

mv ged.json ingredients.json || die "Unable to move changes to ingredients.json"



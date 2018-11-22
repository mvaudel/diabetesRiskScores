#!/usr/bin/env bash

##
# Script used to merge multiple files produced from the mgh biobank into a single vcf.
##


## Parameters

# Repository home directory
repo=/mnt/work/marc/grs/diabetesRiskScores

# Directory with the plink executables
plinkFolder=/mnt/work/marc/tools/plink1.90b3.36

# Directory with the plink files
directory=/mnt/archive/marc/partners/grs/josep

# destination stem
destinationStem=/mnt/archive/marc/partners/grs/merged/partnersGrs


## Script

# Create resource files
$repo/R/plinkFileset.R $directory "T2D_SNPs" $directory/fileset

# Fix snp ids
for file in "$directory"/*.bed
do
    stem="${file%.*}"
    $plinkFolder/plink \
        --bfile $stem \
        --update-name {$stem}.snplist \
        --make-bed \
        --out {$destinationStem}_rsid
done
exit 1
# Merge
$plinkFolder/plink \
        --merge-list $directory/fileset \
        --out $destinationStem

# Convert to vcf
$plinkFolder/plink \
        --bfile $destinationStem \
        --recode vcf \
        --out $destinationStem



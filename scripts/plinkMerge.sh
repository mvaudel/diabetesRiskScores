#!/usr/bin/env bash

##
# This merges multiple plink files into a single one.
##


## Parameters

# Repository home directory
repo=/mnt/work/marc/grs/diabetesRiskScores

# Directory with the plink executables
plinkFolder=/mnt/work/marc/tools/plink1.90b3.36

# Directory with the plink files
directory=/mnt/archive/marc/partners/grs/josep

# destination file
destination=/mnt/archive/marc/partners/grs/merged/partnersGrs


## Script

# Create file set
$repo/R/plinkFileset.R $directory "T2D_SNPs" $directory/fileset

# Merge
$plinkFolder/plink \
        --merge-list $directory/fileset \
        --out $destination/partnersGrs

# Convert to vcf
$plinkFolder/plink \
        --bfile $destination/partnersGrs \
        --recode vcf \
        --out $destination/partnersGrs



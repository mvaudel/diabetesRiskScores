#!/usr/bin/env Rscript

##
# This scipt creates a list of plink stem files from a folder. The folder should not contain other folders.
# 
# @param 1 the folder to process
# @param 2 the prefix of the files to process
# @param 3 the file where to save the list of stem
##


## Parameters

# Import command line arguments
args <- commandArgs(TRUE)

# Folder with the files
fileFolder <- args[1]

# Prefix
prefix <- args[2]

# Output file
outputFile <- args[3]


## Script

# List files
files <- list.files(fileFolder)

# Filter by prefix
files <- files[startsWith(files, prefix) & !endsWith(files, "log")]

# Extract stem
files <- unique(substring(text = files, first = 1, last = regexpr("\\.[^\\.]*$", files) - 1))
files <- paste(fileFolder, files, sep = "/")

# Export
writeLines(files, outputFile)

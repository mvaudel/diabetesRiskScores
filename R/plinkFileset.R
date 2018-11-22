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


## Functions

getSnpIds <- function(x) {
    
    return(paste(paste(x[3], x[4], sep = ":"), x[2], sep = " "))
    
}


## Script

# List files
files <- list.files(fileFolder)

# Filter by prefix
files <- files[startsWith(files, prefix) & !endsWith(files, "log")]

# Extract stem
stems <- unique(substring(text = files, first = 1, last = regexpr("\\.[^\\.]*$", files) - 1))
stemFiles <- file.path(fileFolder, paste0(stems, "_rsid"))

# Export
writeLines(text = stemFiles, con = outputFile)

# Extract snp_ids
snpIds <- unlist(lapply(strsplit(x = stems, split = "\\."), FUN = getSnpIds))

for (i in 1:length(stems)) {
    
    outputFile <- file.path(fileFolder, paste0(stems[i], ".snplist"))
    writeLines(text = snpIds[i], con = outputFile)
    
}





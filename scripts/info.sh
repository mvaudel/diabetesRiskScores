
parallel --jobs 22 --eta echo java -cp bin/drs-0.0.1/drs-0.0.1.jar no.uib.drs.cmd.InfoFile -g /mnt/archive/HARVEST/genotypes-base/imputed/all/{}.vcf.gz -o /mnt/archive/marc/scores/info/moba/harvest/{} -t TYPED -f 0 -s INFO ::: {1..22}


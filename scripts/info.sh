
parallel --jobs 22 --eta java -cp bin/drs-0.0.1/drs-0.0.1.jar no.uib.drs.cmd.InfoFile -g /mnt/archive/HARVEST/genotypes-base/imputed/all/{}.vcf.gz -o /mnt/archive/marc/scores/info/moba/harvest/{}.gz -t TYPED -f 0 -s INFO ::: {1..22}
parallel --jobs 22 --eta java -cp bin/drs-0.0.1/drs-0.0.1.jar no.uib.drs.cmd.InfoFile -g /mnt/archive/ROTTERDAM1/genotypes-base/imputed/all/{}.vcf.gz -o /mnt/archive/marc/scores/info/moba/rotterdam1/{}.gz -t TYPED -f 0 -s INFO ::: {1..22}

java -cp bin/drs-0.0.1/drs-0.0.1.jar no.uib.drs.cmd.InfoFile -g /mnt/archive/HARVEST/genotypes-base/imputed/all/X.vcf.gz -o /mnt/archive/marc/scores/info/moba/harvest/X.gz -t TYPED -f 0 -s INFO
java -cp bin/drs-0.0.1/drs-0.0.1.jar no.uib.drs.cmd.InfoFile -g /mnt/archive/ROTTERDAM1/genotypes-base/imputed/all/X.vcf.gz -o /mnt/archive/marc/scores/info/moba/rotterdam1/X.gz -t TYPED -f 0 -s INFO

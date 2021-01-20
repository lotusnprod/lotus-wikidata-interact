zcat platinum.tsv.gz | head -n 1 > sorted_platinum.tsv 
pigz -cd platinum.tsv.gz | tail -n +2 | sort -k12,12 -t$'\t' >> sorted_platinum.tsv 

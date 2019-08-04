ALG=$1
HASH=$2
ADD_PARAM=$3
in_dir=resources/test-assets
out_dir=resources/encrypted-assets/$ALG-$HASH
echo Generate $out_dir
mkdir -p $out_dir
for file_name in $(find $in_dir -mindepth 1 -type d -printf '%P\n'); 
do 
    mkdir -p $out_dir/$file_name
done

for file_name in $(find $in_dir -mindepth 1 -type f -printf '%P\n'); 
do 
    openssl enc -$ALG -pass pass:test -e -md $HASH -nosalt -in=$in_dir/$file_name -out=$out_dir/$file_name $ADD_PARAM
done

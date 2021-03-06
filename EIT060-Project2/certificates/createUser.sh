ABSOLUTE_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/$(basename "${BASH_SOURCE[0]}")"
CA_CERT="${ABSOLUTE_PATH}""CA/certificate.cer"
CA_PEM="$ABSOLUTE_PATH""CA/cacert.pem"
CA_KEY="${ABSOLUTE_PATH}""CA/private/cakey.pem"
KEYSTORE_FILE="keystore"
TRUSTSTORE_FILE="truststore"
KEYSTORE_PASS=
USERID=
PASSWD=
TRUST_PASSWD=

read -p "userID: " -r USERID


KEYSTORE_PATH="$ABSOLUTE_PATH""Users/${USERID}/${USERID}""_keystore"
TRUSTSTORE_PATH="$ABSOLUTE_PATH""Users/${USERID}/${USERID}""_truststore"

CER_PATH="$ABSOLUTE_PATH/Users/${USERID}/${USERID}.cer"
CSR_PATH="$ABSOLUTE_PATH/Users/${USERID}/${USERID}.csr"

mkdir -p "${ABSOLUTE_PATH}/Users/$USERID"

keytool -import -file "$CA_CERT" -alias "$TRUSTSTOER_PATH"  -keystore "$TRUSTSTORE_PATH"

keytool -keystore "$KEYSTORE_PATH" -genkey -alias "$KEYSTORE_PATH"


keytool -keystore "$KEYSTORE_PATH" -certreq -alias "$KEYSTORE_PATH" -keyalg rsa -file "$CSR_PATH"


openssl x509 -req -CA "$CA_PEM" -CAkey "$CA_KEY" -in "$CSR_PATH" -out "$CER_PATH" -days 365 -CAcreateserial

keytool -import -keystore "$KEYSTORE_PATH" -file $CA_PEM -alias cert

keytool -import -keystore "$KEYSTORE_PATH" -file "$CER_PATH" -alias "$KEYSTORE_PATH"



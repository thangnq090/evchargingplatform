#!/usr/bin/env bash
# =============================================================================
# JWT Key Pair Generation Script for EV Charging Platform
# =============================================================================
# Generates RS256 key pair for JWT signing and verification.
# Creates:
#   - private-key.pem (RSA 2048-bit private key, PKCS#8 format)
#   - public-key.pem (RSA 2048-bit public key, PKIX format)
#   - jwks.json (JWK Set for JWKS endpoint)
#
# Usage: ./scripts/generate-jwt-keys.sh [output-directory]
# Default output directory: ./keys
# =============================================================================

set -euo pipefail

# Default output directory
OUTPUT_DIR="${1:-./keys}"

# Key parameters
KEY_ALGO="RSA"
KEY_SIZE=2048
KEY_USE="sig"
KEY_ALG="RS256"
KEY_KID="evcharging-$(date +%s)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if openssl is available
if ! command -v openssl &> /dev/null; then
    log_error "openssl is not installed. Please install it first."
    exit 1
fi

# Check if jq is available for JWKS generation
if ! command -v jq &> /dev/null; then
    log_warning "jq is not installed. JWKS generation will be skipped."
    JQ_AVAILABLE=false
else
    JQ_AVAILABLE=true
fi

log_info "Generating RS256 key pair for JWT signing..."
log_info "Output directory: $OUTPUT_DIR"
log_info "Key size: $KEY_SIZE bits"
log_info "Algorithm: $KEY_ALG"
log_info "Key ID: $KEY_KID"

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Generate private key (PKCS#8 format)
PRIVATE_KEY_PATH="$OUTPUT_DIR/private-key.pem"
log_info "Generating private key: $PRIVATE_KEY_PATH"
openssl genpkey -algorithm "$KEY_ALGO" -pkeyopt rsa_keygen_bits:"$KEY_SIZE" -out "$PRIVATE_KEY_PATH"

# Set restrictive permissions on private key
chmod 600 "$PRIVATE_KEY_PATH"

# Extract public key (PKIX format)
PUBLIC_KEY_PATH="$OUTPUT_DIR/public-key.pem"
log_info "Extracting public key: $PUBLIC_KEY_PATH"
openssl pkey -in "$PRIVATE_KEY_PATH" -pubout -out "$PUBLIC_KEY_PATH"

# Generate JWKS if jq is available
if [ "$JQ_AVAILABLE" = true ]; then
    JWKS_PATH="$OUTPUT_DIR/jwks.json"
    log_info "Generating JWKS: $JWKS_PATH"

    # Extract modulus and exponent from public key
    MODULUS=$(openssl rsa -pubin -in "$PUBLIC_KEY_PATH" -noout -modulus | cut -d'=' -f2)
    EXPONENT=$(openssl rsa -pubin -in "$PUBLIC_KEY_PATH" -noout -text | grep -A1 "Exponent:" | tail -1 | tr -d ' ' | sed 's/^(//;s/)$//')

    # Convert hex to base64url
    MODULUS_B64=$(echo "$MODULUS" | xxd -r -p | base64 | tr '+/' '-_' | tr -d '=')
    EXPONENT_B64=$(printf "%x" "$EXPONENT" | xxd -r -p | base64 | tr '+/' '-_' | tr -d '=')

    # Generate JWKS
    cat > "$JWKS_PATH" <<EOF
{
  "keys": [
    {
      "kty": "RSA",
      "use": "$KEY_USE",
      "alg": "$KEY_ALG",
      "kid": "$KEY_KID",
      "n": "$MODULUS_B64",
      "e": "$EXPONENT_B64"
    }
  ]
}
EOF

    log_success "JWKS generated: $JWKS_PATH"
fi

# Display key information
log_success "Key pair generated successfully!"
echo
echo "Private key: $PRIVATE_KEY_PATH"
echo "Public key:  $PUBLIC_KEY_PATH"
if [ "$JQ_AVAILABLE" = true ]; then
    echo "JWKS:        $JWKS_PATH"
fi
echo

# Show public key fingerprint
log_info "Public key fingerprint (SHA-256):"
openssl pkey -pubin -in "$PUBLIC_KEY_PATH" -outform DER | openssl dgst -sha256 -binary | base64 | tr '+/' '-_' | tr -d '='

echo
log_info "To use these keys in Spring Boot, add to application.yml:"
echo
cat <<EOF
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: \${JWT_JWKS_URI:http://localhost:8080/api/v1/identity/.well-known/jwks.json}
      authorizationserver:
        issuer-uri: \${JWT_ISSUER_URI:http://localhost:8080/realms/evcharging}
        jws-signature-algorithm: RS256
EOF

echo
log_warning "IMPORTANT: Keep the private key secure! Never commit it to version control."
log_warning "Add '$OUTPUT_DIR/' to your .gitignore file."
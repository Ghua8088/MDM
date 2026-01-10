#!/bin/bash
set -e

echo "Installing MDM-CLI..."

# 1. Install Dir
INSTALL_DIR="$HOME/.mdm/bin"
mkdir -p "$INSTALL_DIR"

# 2. Check Source
JAR_SOURCE="./target/mdm-cli-1.0-SNAPSHOT.jar"
if [ ! -f "$JAR_SOURCE" ]; then
    echo "Error: Build artifact not found. Run 'mvn package' first."
    exit 1
fi

# 3. Copy
cp "$JAR_SOURCE" "$INSTALL_DIR/mdm-cli.jar"
echo "Copied JAR to $INSTALL_DIR"

# 4. Create Shim
SHIM="$INSTALL_DIR/mdm"
echo '#!/bin/bash' > "$SHIM"
echo 'java -jar "$(dirname "$0")/mdm-cli.jar" "$@"' >> "$SHIM"
chmod +x "$SHIM"
echo "Created executable shim."

# 5. Path Advice
if [[ ":$PATH:" != *":$INSTALL_DIR:"* ]]; then
    echo "--------------------------------------------------"
    echo "WARNING: $INSTALL_DIR is not in your PATH."
    echo "Add the following line to your ~/.bashrc or ~/.zshrc:"
    echo "export PATH=\"\$PATH:$INSTALL_DIR\""
    echo "--------------------------------------------------"
else
    echo "PATH looks good."
fi

echo "Success! Run 'mdm --help' to start."

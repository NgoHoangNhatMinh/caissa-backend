SRC_DIR := src
OUT_DIR := out

# Exclude controller and Spring Boot files
SOURCE := $(shell find $(SRC_DIR) -name "*.java" ! -path "$(SRC_DIR)/main/java/com/example/caissa_bot_backend/controller/*" ! -name "*CaissaBotBackendApplication.java" ! -path "$(SRC_DIR)/test/*")

# Fully qualified class names
MAIN_CLASS := com.example.caissa_bot_backend.Main
ENGINE_CLASS := com.example.caissa_bot_backend.Engine
PERFT_CLASS := com.example.caissa_bot_backend.Perft

# Compile and run
all:
	@echo "Compiling program..."
	@mkdir -p $(OUT_DIR)
	@javac -d $(OUT_DIR) $(SOURCE)
	@echo "Starting program..."
	@java -cp $(OUT_DIR) $(MAIN_CLASS)

compile:
	@echo "Compiling program..."
	@mkdir -p $(OUT_DIR)
	@javac -d $(OUT_DIR) $(SOURCE)

run:
	@echo "Starting program..."
	@java -cp $(OUT_DIR) $(MAIN_CLASS)

clean:
	@echo "Cleaning folder..."
	@rm -rf $(OUT_DIR)

test:
	@echo "Running tests..."
	@echo "Verify results against: https://www.chessprogramming.org/Perft_Results"
	@mkdir -p $(OUT_DIR)
	@javac -d $(OUT_DIR) $(SOURCE)
	@java -cp $(OUT_DIR) $(PERFT_CLASS) $(DEPTH)

engine:
	@echo "Running engine..."
	@mkdir -p $(OUT_DIR)
	@javac -d $(OUT_DIR) $(SOURCE)
	@java -cp $(OUT_DIR) $(ENGINE_CLASS) $(DEPTH)

play:
	@echo "Playing against engine..."
	@mkdir -p $(OUT_DIR)
	@javac -d $(OUT_DIR) $(SOURCE)
	@java -cp $(OUT_DIR) $(MAIN_CLASS) $(COLOR)
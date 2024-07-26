# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# ------------------------------------------------------------------------------------------------

# Target for different test suites

ifeq ($(TEST_SUITES), ics2023)
	target := cpu-test am-test alu-test coremark dhrystone microbench demo typing-game bad-apple fceux nanos-lite
endif

$(target): $(OBJECT)
	@make -f $(SIM_ICS_PA_DIR)/scripts/ics2023.mk $@ TOP=$(TOP) TEST_SUITES=$(TEST_SUITES) OUTPUT_DIR=$(OUTPUT_DIR) \
		BUILD_DIR=$(BUILD_DIR) OBJECT=$(OBJECT) RESULT=$(RESULT)

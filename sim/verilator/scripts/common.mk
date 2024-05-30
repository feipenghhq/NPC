# -----------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# -----------------------------------------------------------------------------------------------

# Common function for makefile

OUTPUT_DIR ?=
BUILD_DIR  ?=
OBJECT     ?=

COLOR_RED   = \033[1;31m
COLOR_GREEN = \033[1;32m
COLOR_NONE  = \033[0m

# Define function to run simulation. Usage: $(call run_sim,image,elf,suite,test,dut,maxlen)
define run_sim
	/bin/echo -e " \
		run:\n\tcd $(OUTPUT_DIR) && $(BUILD_DIR)/$(OBJECT) \
		--image $(1) --elf $(2) --suite $(3) --test $(4) --dut $(5) --ref $(REF_SO) \
		" \
		>> $(OUTPUT_DIR)/makefile.$(4)
	@if make -s -f $(OUTPUT_DIR)/makefile.$(4); then \
		printf "[%$(6)s] $(COLOR_GREEN)%s!$(COLOR_NONE)\n" $(4) PASS >> $(RESULT); \
	else \
		printf "[%$(6)s] $(COLOR_RED)%s!$(COLOR_NONE)\n" $(4) FAIL >> $(RESULT); \
	fi
	-@rm $(OUTPUT_DIR)/makefile.$(4)
endef


# ------------------------------------------------------------------------------------------------
# Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
#
# Project: NRC
# Author: Heqing Huang
# Date Created: 12/18/2023
# ------------------------------------------------------------------------------------------------

TEST_SUITES = ics-am-cpu-test

ifeq ($(AM_KERNELS_HOME),)
$(error "Please set AM_KERNELS_HOME")
else
AM_KERNELS_PATH = $(AM_KERNELS_HOME)
endif

IMAGE_PATH = $(AM_KERNELS_PATH)/tests/cpu-tests/build

TEST_NAME_MAX_LEN = 26

# get all the tests in the test suites
ICS_AM_CPU_TESTS = $(basename $(notdir $(shell find $(IMAGE_PATH) -name "*.bin")))

ics_am_cpu_tests: $(ICS_AM_CPU_TESTS)
	@cat $(RESULT)

$(ICS_AM_CPU_TESTS): $(OBJECT)
	$(call run_sim,$(IMAGE_PATH)/$@.bin,$(TEST_SUITES),$@,$(TOP))


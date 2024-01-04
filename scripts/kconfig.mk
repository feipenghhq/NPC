
MCONF = kconfig-mconf
CONF  = kconfig-conf

menuconfig:
	@mkdir -p $(WORK_DIR)/include/generated
	@mkdir -p $(WORK_DIR)/include/config
	@$(MCONF) $(WORK_DIR)/Kconfig
	@$(CONF) --silentoldconfig $(WORK_DIR)/Kconfig


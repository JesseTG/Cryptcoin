
doc:
	javadoc -d ./doc -doctitle "CryptCoin" -windowtitle "CryptCoin" -sourcepath ./protocol.jar:./src -classpath ./protocol.jar:./src \
	-tag O:a:"Worst-Case Complexity:" \
	com.starkeffect.cryptcoin.protocol \
	com.starkeffect.util \
	jtg.util \
	jtg.cse260 \
	jtg.cse260.cryptcoin \
	jtg.cse260.cryptcoin.cli \
	jtg.cse260.cryptcoin.cli.command \
	jtg.cse260.cryptcoin.block \
	jtg.cse260.cryptcoin.gui \
	jtg.cse260.cryptcoin.gui.windows \
	jtg.cse260.cryptcoin.gui.tabs \
	jtg.cse260.cryptcoin.node

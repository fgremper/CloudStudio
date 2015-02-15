note
	description: "Root class of the bank account demo."

class
	APPLICATION

inherit
	ARGUMENTS

create
	make

feature {NONE} -- Initialization

	make
			-- Run the bank application.
		do
			print ("Welcome!%N")
		end

end

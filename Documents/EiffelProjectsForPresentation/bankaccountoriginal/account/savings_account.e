note
	description: "This class represents a savings account."

class
	SAVINGS_ACCOUNT

inherit
	ACCOUNT

create
	make_with_balance_and_owner

feature -- Operations

	pay_out_interest
			-- calculate interest
		do
			balance := balance * 1.05
			log.put_right("Paid out 5 percent interest%N")
		end

end

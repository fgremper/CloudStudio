note
	description: "This class represents a bank account."

class
	ACCOUNT

create
	make

feature {NONE} --Initialization

	make
			-- Creation procedure
		do
			balance := 0
		end

feature -- Operations

	deposit(a_amount: DOUBLE)
			-- deposit the amount 'a_amount' to the account
		do
			balance := balance + a_amount
		end


	withdraw(a_amount: DOUBLE)
			-- withdraw the amount 'a_amount' from the account
		do
			balance := balance - a_amount
		end


feature -- Account properties

	balance: DOUBLE
			-- the balance of the account

	owner: CUSTOMER
			-- the owner of the acccount

end

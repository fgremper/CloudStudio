note
    description: "This class represents a bank account."

class
    ACCOUNT

create
    make_with_balance_and_owner

feature {NONE} --Initialization

    make_with_balance_and_owner(a_amount: DOUBLE; a_owner: CUSTOMER)
            -- Creates a new account with balance `a_balance' and owner `a_owner'
        do
            owner := a_owner
            balance := a_amount
            create log.make
        end

feature -- Operations

    deposit(a_amount: DOUBLE)
            -- deposit the amount 'a_amount' to the account
        do
            balance := balance + a_amount
            log.put_right("Deposit: " + a_amount.out + "%N")
        end

    withdraw(a_amount: DOUBLE)
            -- withdraw the amount 'a_amount' from the account
        do
            balance := balance - a_amount
            log.put_right("Withdraw: " + a_amount.out + "%N")
        end

    print_log
            -- print log
        do
            -- TODO!
        end

feature -- Account properties

    balance: DOUBLE
            -- the balance of the account

    owner: CUSTOMER
            -- the owner of the acccount

    log: LINKED_LIST[STRING]
            -- log of transactions

end

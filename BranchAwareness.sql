ALL BRANCHES X ALL USERS and now left join the results




SELECT branchxuser.branch, branchxuser.username, branch.commit FROM 

(
	SELECT allbranches.branch AS branch, allusers.username FROM

	(
		SELECT DISTINCT branch FROM branches
		WHERE repositoryalias = 'test'
	) AS allbranches

	CROSS JOIN

	(
		SELECT DISTINCT username FROM useraccess
		WHERE repositoryalias = 'test'
	) AS allusers
	
) AS branchxuser

LEFT OUTER JOIN 

(SELECT branch, username FROM branches
WHERE repositoryalias = 'test') AS branch

ON branchxuser.branch = branches.branch
AND branchxuser.username = branches.username

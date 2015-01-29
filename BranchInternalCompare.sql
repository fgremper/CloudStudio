SELECT myfilename, mysha, theirusername, theirfilename, theirsha FROM (

	# all possible files of me X (all possible files X all others)
	SELECT me.filename AS myfilename, me.sha AS mysha, them.username AS theirusername, them.filename AS theirfilename, them.sha AS theirsha FROM (

		# all the files X me
		SELECT filelist.filename AS filename, f.sha AS sha FROM filelist
		LEFT OUTER JOIN (
			SELECT * FROM files
			WHERE files.username = 'david' # my user
			AND (committed = 'uncommitted' OR committed = 'both') # committed or uncommitted
			AND branch = 'master' # my branch
			AND repositoryalias = 'test' # repository
		) AS f ON filelist.filename = f.filename

	) AS me

	CROSS JOIN

	(

		# all the files of all the other people
		SELECT filelistxusers.username, filelistxusers.filename, f.sha FROM (

			# all the filenames X all the users
			SELECT u.username AS username, filelist.filename AS filename FROM filelist
			CROSS JOIN (SELECT DISTINCT username FROM useraccess WHERE repositoryalias = 'test') AS u # repository
			WHERE filelist.repositoryalias = 'test' AND filelist.branch = 'master' # repository, compare to branch

		) as filelistxusers
		LEFT OUTER JOIN (

			# the actual files
			SELECT * FROM files
			WHERE (committed = 'uncommitted' OR committed = 'both') # committed or uncommitted
			AND branch = 'master' # compare to branch
			AND repositoryalias = 'test' # repository

		) AS f ON filelistxusers.filename = f.filename AND filelistxusers.username = f.username

	) AS them
	WHERE me.filename = them.filename

) AS t

WHERE (t.mysha IS NULL AND t.theirsha IS NOT NULL) OR (t.mysha IS NOT NULL AND t.theirsha IS NULL) OR (t.mysha IS NOT NULL AND t.theirsha IS NOT NULL AND t.mysha <> t.theirsha) # complicated cause comparison with null always returns null
ORDER BY t.myfilename, t.theirusername

% This is an example of how to modify the style of your
% thesis.  Check the guidelines for your school/university
% on the required format for a PhD thesis.

% First identify the class:

\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{mythesis}

\DeclareOption*{\PassOptionsToClass{\CurrentOption}{book}}

\ProcessOptions

\LoadClass{book}

% Make the captions in your floats of the form:
% Fig. 1
% instead of:
% Figure 1

\usepackage{color}

\renewcommand{\figurename}{Fig.}
\renewcommand{\tablename}{Tab.}

% Redefining the \section command:

%\renewcommand{\section}{\@startsection
%{section}%                   % the name
%{1}%                         % the level
%{0mm}%                       % the indent
%{-\baselineskip}%            % the before skip
%{0.5\baselineskip}%          % the after skip
%{\normalfont\large\itshape}} % the style

%\renewcommand{\subsection}{\@startsection
%{subsection}%                % the name
%{2}%                         % the level
%{0mm}%                       % the indent
%{-\baselineskip}%            % the before skip
%{0.5\baselineskip}%          % the after skip
%{\normalfont\large\itshape}} % the style

% Redefining the chapter heading styles

% Numbered chapter heading style:

\renewcommand{\@makechapterhead}[1]{%
  \vspace*{50\p@}%
  {\parindent \z@ \raggedright \normalfont
    \vspace{5pt}%                                    % add some vertical space
    \ifnum \c@secnumdepth >\m@ne
        \huge\scshape \color{black} \@chapapp\space \thechapter % Chapter followed by number
        \par\nobreak
        \vskip 20\p@
    \fi
    \interlinepenalty\@M
    \huge 
    %\color{darkgrey} 
    \scshape #1\par               % chapter title
    \vspace{5pt}%                                    % add some vertical space
    \nobreak
    \vskip 40\p@
  }}

% Unnumbered chapter heading style:

\renewcommand{\@makeschapterhead}[1]{%
  \vspace*{50\p@}%
  {\parindent \z@ \raggedright
    \normalfont
    \vspace{5pt}%                                    % add some vertical space
    \interlinepenalty\@M
    \huge 
    %\color{darkgrey} 
    \scshape #1\par                            % chapter title
    \vspace{5pt}%                                    % add some vertical space
    \nobreak
    \vskip 40\p@
  }}

% ----------------------------------------------------------------------
% Theorems
\newcommand{\listofdefinitions}{
  \chapter*{List of Definitions}
  \@starttoc{lod}
}

\def\l@definition#1#2{\@dottedtocline{1}{1.5em}{2.3em}{#1}{#2}}

\setcounter{secnumdepth}{2}
\newcounter{definition}[chapter]
\renewcommand\thedefinition{\thechapter.\@arabic\c@definition}
\newcommand\theHdefinition{\thechapter.\@arabic\c@definition}

\newenvironment{Definition}[1][TODO]{
  \begin{center}
  \begin{boxedminipage}{0.9\textwidth}
    \refstepcounter{definition}%
    {\bf Definition \thedefinition: #1}
    \vskip 0.2em
    \hrule
    \vskip 0.5em
    \addcontentsline{lod}{definition}{\protect\numberline{\thedefinition}#1}%
}{
  \end{boxedminipage}
  \end{center}
}

% end of file:
\endinput
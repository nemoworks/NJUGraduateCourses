#Q = {start,cp,reset_all,reset_first,search1,search2,cmp,clean,accept,accept2,accept3,accept4,halt_accept,reject,reject2,reject3,reject4,reject5,halt_reject}
#S = {0,1}
#G = {0,1,_,T,r,u,e,F,a,l,s}
#q0 = start
#B = _
#F = {halt_accept}
#N = 2

; transition functions
; start
start __ __ ** accept
start ** ** ** cp

; cp string to the 2nd tape
cp 0_ 00 rr cp
cp 1_ 11 rr cp
cp __ __ ll reset_all

; reset_all: move head0 and head1 to the left
reset_all ** ** ll reset_all
reset_all __ __ rr search1

; search: search the mid
; head0 move twice while head1 move once.
search1 ** ** r* search2
search2 ** ** rr search1
search1 _* _* l* reset_first
search2 _* _* l* clean     ; length is odd

;clean: clean the tape
clean ** _* l* clean
clean _* _* ** reject

; reset_first
reset_first ** ** l* reset_first
reset_first _* _* r* cmp

; compare until head1 hit blank
cmp 00 __ rr cmp
cmp 01 __ ** reject
cmp 10 __ ** reject
cmp 11 __ rr cmp
cmp *_ *_ ** accept

; accept: first, clean the tape then write True
accept ** _* r* accept
accept _* T* r* accept2
accept2 _* r* r* accept3
accept3 _* u* r* accept4
accept4 _* e* ** halt_accept

; reject: first, clean the tape then write False
reject ** _* r* reject
reject _* F* r* reject2
reject2 _* a* r* reject3
reject3 _* l* r* reject4
reject4 _* s* r* reject5
reject5 _* e* ** halt_reject





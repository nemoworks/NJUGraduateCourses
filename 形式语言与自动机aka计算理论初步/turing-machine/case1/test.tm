#Q = {0,init1,init2,init3,init4,test,cmp,grow,forward,reset1,reset2,accept,accept2,accept3,accept4,halt_accept,reject,reject2,reject3,reject4,reject5,halt_reject}
#S = {0}
#G = {0,X,Y,Z,_,T,r,u,e,F,a,l,s}
#q0 = 0
#B = _
#F = {halt_accept}
#N = 3

; transition functions
; state 0
0 ___ ___ *** reject  ;empty string
0 0__ *__ r** test
test ___ ___ l** accept   ;length=1
test 0__ 0__ l** init1     ;length>1,we start 

;init: write 00 to tape1, mark the begin in tape2
init1 0__ 00_ *rr init2
init2 0__ 00Y *ll init3
init3 00_ *** **l init4
init4 **_ **X **r cmp

; cmp
cmp 00* *** rrr cmp
cmp __* *** l** accept
cmp _0* *** l** reject
cmp 0_* 0ZY **l reset2

;reset2 
reset2 *** *** **l reset2
reset2 **X **X **r grow


;grow
grow 0Z_ 0Z_ *rr grow
grow 0__ 00_ *rr grow
grow 0_Y 0__ *l* reset1

;reset1
reset1 *0* *0* *l* reset1
reset1 *Z* *0* *** forward

;forward
forward 00_ 00_ **r forward
forward 00Y 00Y *** cmp

;accept
accept 0** _** l** accept
accept _** T** r** accept2
accept2 _** r** r** accept3
accept3 _** u** r** accept4
accept4 _** e** *** halt_accept

;reject
reject 0** _** l** reject
reject _** F** r** reject2
reject2 _** a** r** reject3
reject3 _** l** r** reject4
reject4 _** s** r** reject5
reject5 _** e** *** halt_reject












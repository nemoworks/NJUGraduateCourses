function newweight=adjustweight(est_loc,edgelist,range)
% Function newweight=adjustweight(est_loc,edgelist,range)
% Adjust the weight (stiffness) of each edge
% Input 
%   -- est_loc: estimated location
%   -- edgelist: edge list with original edge weights
%   -- range: transmission range
% Output
%   -- newweight: the adjusted weight

%% You should write your own version of this function
newweight=edgelist(:,3);
n=size(edgelist,1);
for i=1:n
    from=est_loc(edgelist(i,1));
    to=est_loc(edgelist(i,2));
    dist=sum((from-to).^2).^0.5;
    newweight(i)=edgelist(i,3)*dist/range;
end


end
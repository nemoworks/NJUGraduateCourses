function loc=balancenet(n,edgelist,anchor)
%Function loc=balancenet(n,edgelist,anchor)
% find out the balanced locations for all nodes 
% Input:
%    -- n: number of nodes
%    -- edgelist: edge list with the third column to be the weights
%    -- anchor: anchor list: first column is the node id, second and third
%               are corrdinates for the anchors.
% Output:
%    -- loc: estimated locations

%% You should write your own version of this function
loc=zeros(n,2);



%% Generate the adjacency matrix from edgelist, if you are not sure, you can
%  look at the getedges.m sample
adjmatrix=sparse(edgelist(:,1),edgelist(:,2),edgelist(:,3),n,n);


%% Generate the Laplacian matrix from the adjacency matrix
diamatrix=diag(adjmatrix*ones(n,1));

laplacian=diamatrix - adjmatrix;



%% Handle special cases for all anchor nodes
eyematrix=eye(n);
for idx=1:size(anchor,1)
    i=anchor(idx,1);
    laplacian(i,:)=eyematrix(i,:);
    loc(i,:)=anchor(idx,2:3);
end

%% Inverse the modified Laplacian matrix 


%% Use the inverse matrix to calculate estimated positions
if(isnan(rcond(laplacian)) || rcond(laplacian)<1e-10)
    loc=NaN;
    return
end
est=laplacian\loc;
loc=est;

end
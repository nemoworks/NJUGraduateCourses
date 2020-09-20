function mds_loc=mds(n, edgelist)
% Function mds_loc=mds(edgelist)
% calculate the mds relative location given the edgelist
% Input 
%    -- n: number of nodes in the network
%    -- edgelist: the list of edges, with only the tail and head node id
% Output
%    -- mds_loc: the estimated relative locations using MDS method, NaN
%    means not connected

%% You should write your own version of this function
mds_loc=zeros(n,2);


%% Calculate the shortest paths for all pair of nodes
s = edgelist(:,1).';
t = edgelist(:,2).';
g = graph(s,t);
D = distances(g);

%% Judge whether nodes are connected
if(sum(any(isinf(D)))~=0)
    mds_loc=NaN;
    return
end

%% Calculate the matrix Y from shortest path distance
o = ones(size(D));
d = diag(D*D.');
R = diag(d)*o;
C = R.';
D2 = D.^2;
Ds = ones(size(D))*sum(d);
Y = -0.5*(D2-(1/n)*(R+C)+Ds*(1/n)^2);

%% SVD decomposition for the matrix Y
[U,S,~]=svd(Y);

%% Get the estimated svd location from matrix Y
result=U*sqrt(S);

mds_loc=result(:,1:2);

end
function nodexy=generate_random_network(n,xlimit,ylimit)
% Function nodexy=generate_random_network(n,xlimit,ylimit): generate a 
% random network with uniformly distributed nodes in a rectangular
% Input: 
%   n: Number of nodes
%   xlimit: the sidelength x for the rectangular
%   ylimit: the sidelength y for the rectangular
%
% Output:
%   nodexy: the x and y coordinates for all nodes


nodexy=rand(n,2); %generate random x y
nodexy(:,1)=nodexy(:,1).*xlimit; % scale the coordinates
nodexy(:,2)=nodexy(:,2).*ylimit;
end